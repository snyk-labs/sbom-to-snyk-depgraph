import typer
import json
import time
import sys
import logging
import requests
from uuid import UUID
from cyclonedx.model.bom import Bom
from snyk import SnykClient
from snyk_depgraph import DepGraph
from typing import Optional
from typing import List

# set up logging
logger = logging.getLogger(__name__)
FORMAT = "[%(filename)s:%(lineno)4s - %(funcName)s ] %(message)s"
logging.basicConfig(format=FORMAT)
logger.setLevel(logging.WARN)

# ===== GLOBALS =====

app = typer.Typer(add_completion=False)

# globals
g={}
DEPGRAPH_BASE_TEST_URL = "/test/dep-graph?org="
DEPGRAPH_BASE_MONITOR_URL = "/monitor/dep-graph?org="

package_source = "maven"
dep_graph = DepGraph(package_source, False)

visited = []
visited_temp = []
dep_path_counts = {}

# future use
# transitive_closures = [] 

# ===== METHODS =====

@app.callback()
def main(
    ctx: typer.Context,
    sbom_file: str = typer.Option(
        ...,
        envvar="SBOM_FILE",
        help="Full path to SBOM file"
    ),
    prune_repeated_subdependencies: bool = typer.Option(
        False,
        help="Use if too many repeated sub dependencies causes test or monitor to fail"
    ),
    debug: bool = typer.Option(
        False,
        help="Set log level to debug"
    ),
):
    """ Entrypoint into typer CLI handling """
    if debug:
        logger.debug("*** DEBUG MODE ENABLED ***", file=sys.stderr)
        logger.setLevel(logging.DEBUG)

    logger.debug("sbom_file: " + sbom_file)

    with open(sbom_file) as input_json:
        g['sbom'] = Bom.from_json(data=json.loads(input_json.read()))

        root_component_ref = "unknown"

        if g['sbom']._metadata.component.purl:
            root_component_ref = f"{str(g['sbom']._metadata.component.purl)}"
        elif g['sbom']._metadata.component.name:
            root_component_ref = f"{str(g['sbom']._metadata.component.name)}"
            
        logger.debug(f"{root_component_ref=}")

        sbom_to_depgraph(
            parent_component_ref=root_component_ref, 
            depth=0
        )

        if prune_repeated_subdependencies:
            logger.info("Pruning graph ...")
            time.sleep(2)
            prune()

    return

@app.command()
def print_graph():
    """
    Print Snyk depGraph representation of SBOM
    """
    # dep_graph: DepGraph = g['dep_graph']

    typer.echo(f"{json.dumps(dep_graph.graph(), indent=4)}")
    
    return

@app.command()
def test(
    snyk_token: str = typer.Option(
        None,
        envvar="SNYK_TOKEN",
        help="Please specify your Snyk token"
    ),
    snyk_org_id: str = typer.Option(
        None,
        envvar="SNYK_ORG_ID",
        help="Please specify the Snyk ORG ID to run commands against"
    )
):
    """
    Test SBOM with Snyk
    """
    global dep_graph

    snyk_client = SnykClient(snyk_token)
    response: requests.Response = snyk_client.post(f"{DEPGRAPH_BASE_TEST_URL}{snyk_org_id}", body=dep_graph.graph())

    json_response = response.json()
    print(json.dumps(json_response, indent=4))

    if str(json_response['ok']) == "False":
        typer.echo("exiting with code 1", file=sys.stderr)
        sys.exit(1)

    return

@app.command()
def monitor(
    snyk_token: str = typer.Option(
        None,
        envvar="SNYK_TOKEN",
        help="Please specify your Snyk token"
    ),
    snyk_org_id: str = typer.Option(
        None,
        envvar="SNYK_ORG_ID",
        help="Please specify the Snyk ORG ID to run commands against"
    ),
):
    """
    Monitor SBOM with Snyk
    """
    global dep_graph

    snyk_client = SnykClient(snyk_token)
    response: requests.Response = snyk_client.post(
        f"{DEPGRAPH_BASE_MONITOR_URL}{snyk_org_id}", 
        body=dep_graph.graph()
    )

    json_response = response.json()
    print(json.dumps(json_response, indent=4))

    if str(json_response['ok']) == "False":
        typer.echo("exiting with code 1", file=sys.stderr)
        sys.exit(1)

    return

# Utility Functions
# -----------------

def sbom_to_depgraph(
    parent_component_ref: str,
    depth: int
) -> DepGraph:
    """
    Convert the CDX SBOM components to snyk depgraph to find issues
    """
    global visited
    global visited_temp

    logger.debug(f"processing sbom deps for parent: {parent_component_ref}")
    #dep_graph: DepGraph = g['dep_graph']

    parent_dep_for_depgraph = purl_to_depgraph_dep(purl=parent_component_ref)

    # special entry for the root node of the dep graph
    if depth == 0:
        logger.debug(f"setting degraph root node to: {parent_dep_for_depgraph}")
        dep_graph.set_root_node_package(f"{parent_dep_for_depgraph}")

    children = get_dependencies_from_ref(parent_component_ref)
    logger.debug(f"found child dependencies: {children=}")

    for child in children:
        logger.debug(f"{str(child)=}")
        depgraph_dep = purl_to_depgraph_dep(purl=str(child))

        logger.debug(f"adding pkg {depgraph_dep=} to depgraph")
        dep_graph.add_pkg(depgraph_dep)

        increment_dep_path_count(depgraph_dep)

        logger.debug(f"adding dep {depgraph_dep=} for {parent_dep_for_depgraph=} to depgraph")
        dep_graph.add_dep(child_node_id=depgraph_dep, parent_node_id=parent_dep_for_depgraph)

        visited_temp.append(parent_component_ref)

        # if we've already processed this subtree, then just return
        if child not in visited:
            sbom_to_depgraph(str(child), depth=depth+1)
        # else:
            # future use for smarter pruning
            # account for node in the subtree to count all paths
    
    # we've reach a leaf node and just need to add an entry with empty deps array
    if len(children) == 0:
        dep_graph.add_dep(child_node_id=None, parent_node_id=parent_dep_for_depgraph)
        visited.extend(visited_temp)

        visited_temp = [] 

def get_dependencies_from_ref(dependency_ref) -> List:
    children = []
    for dependency in g['sbom'].dependencies:
        if str(dependency.ref) == dependency_ref:
            logger.debug(f"{dependency.dependencies=}")
            children.extend([str(x.ref) for x in dependency.dependencies])

    return children

def increment_dep_path_count(dep: str):
    """ 
    Keep track of path counts in case we need to prune 
    """
    global dep_path_counts

    dep_path_counts[dep] = dep_path_counts.get(dep, 0) + 1

def prune():
    global dep_graph

    for dep, instances in dep_path_counts.items():
        if instances > 2:
            logger.info(f"pruning {dep} ({instances=})")
            dep_graph.prune_dep(dep)

def purl_to_depgraph_dep(purl: str) -> str:
    """
    Convert purl format string to package@version for snyk
    """
    k = purl.rfind("@")
    logger.debug(f"{purl[:k]=}")
    logger.debug(f"{purl[k+1:]=}")

    if k < 0:
        return purl

    depgraph_dep_name = purl[:k].replace("pkg:maven/", "").replace("/", ":")
    depgraph_dep_version = purl[k+1:]

    k = depgraph_dep_version.find("?")

    if k < 0:
        return purl

    depgraph_dep_version = depgraph_dep_version[:k]
    depgraph_dep = depgraph_dep_name + "@" + depgraph_dep_version

    logger.debug(f"depgraph_dep: {depgraph_dep}")

    return depgraph_dep
    
# ----- app entrypoint ------    
if __name__ == "__main__":
    app()
