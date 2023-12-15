import typer
import json
import sys
import logging
import requests
from uuid import UUID
from cyclonedx.model.bom import Bom
from snyk import SnykClient
from snyk_depgraph import DepGraph
from typing import Optional, List

# set up logging
logger = logging.getLogger(__name__)
FORMAT = "[%(filename)s:%(lineno)4s - %(funcName)s ] %(message)s"
logging.basicConfig(format=FORMAT)
logger.setLevel(logging.WARN)

# ===== GLOBALS =====

app = typer.Typer(add_completion=False)

# globals
g={}
g['DEPGRAPH_BASE_TEST_URL'] = "/test/dep-graph?org="
g['DEPGRAPH_BASE_MONITOR_URL'] = "/monitor/dep-graph?org="
g['package_source'] = "maven"
g['dep_graph']: DepGraph = DepGraph(g['package_source'], False)

# ===== METHODS =====

@app.callback()
def main(
    ctx: typer.Context,
    sbom_file: str = typer.Option(
        None,
        envvar="SBOM_FILE",
        help="Full path to SBOM file"
    ),
    debug: bool = typer.Option(
        False,
        help="Set log level to debug"
    ),
):
    if debug:
        logger.debug("*** DEBUG MODE ENABLED ***", file=sys.stderr)
        logger.setLevel(logging.DEBUG)

    logger.debug("sbom_file: " + sbom_file)

    with open(sbom_file) as input_json:
        g['sbom'] = Bom.from_json(data=json.loads(input_json.read()))

        root_component_ref = "unknown"

        try:
            root_component_ref = f"{str(g['sbom']._metadata.component.name)}@{str(g['sbom']._metadata.component.version)}"
        except:
            try:
                root_component_ref = f"{str(g['sbom']._metadata.component.name)}"
            except:
                pass
            
        logger.debug(root_component_ref)

        cdx_sbom_to_depgraph(parent_node_ref=root_component_ref)

    return

@app.command()
def print_graph():
    """
    Print Snyk depGraph representation of SBOM
    """
    dep_graph: DepGraph = g['dep_graph']

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
    dep_graph: DepGraph = g['dep_graph']
    snyk_client = SnykClient(snyk_token)
    response: requests.Response = snyk_client.post(f"{g['DEPGRAPH_BASE_TEST_URL']}{snyk_org_id}", body=dep_graph.graph())

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
    snyk_project_name: Optional[str] = typer.Option(
        None,
        envvar="SNYK_PROJECT_NAME",
        help="Specify a custom Snyk project name. By default Snyk will use the name of the root node."
    ),
):
    """
    Monitor SBOM with Snyk
    """
    dep_graph: DepGraph = g['dep_graph']
    snyk_client = SnykClient(snyk_token)
    response: requests.Response = snyk_client.post(f"{g['DEPGRAPH_BASE_MONITOR_URL']}{snyk_org_id}", body=dep_graph.graph())

    json_response = response.json()
    print(json.dumps(json_response, indent=4))

    if str(json_response['ok']) == "False":
        typer.echo("exiting with code 1", file=sys.stderr)
        sys.exit(1)

    return

# Utility Functions
# -----------------

def cdx_sbom_to_depgraph(
    parent_node_ref: str
) -> DepGraph:
    """
    Convert the CDX SBOM components to snyk depgraph to find issues
    """
    logger.debug(f"processing depgraph from root: {parent_node_ref}")
    dep_graph: DepGraph = g['dep_graph']

    parent_dep_for_depgraph = purl_to_depgraph_dep(purl=parent_node_ref)

    component_purls = get_sbom_component_purls()

    dep_graph.set_root_node_package(f"{parent_dep_for_depgraph}")

    for purl in component_purls:
        depgraph_dep = purl_to_depgraph_dep(purl=purl)

        logger.debug(f"adding pkg {depgraph_dep=} to depgraph")
        dep_graph.add_pkg(depgraph_dep)

        logger.debug(f"adding dep {depgraph_dep=} for {parent_dep_for_depgraph=} to depgraph")
        dep_graph.add_dep(depgraph_dep, parent_dep_for_depgraph)
        dep_graph.add_dep(child_node_id = None, parent_node_id = depgraph_dep)

def get_sbom_component_purls() -> List[str]:
    """
    Return the list of component Purl strings
    """
    component_purls = []

    for component in g['sbom'].components:
        component_purls.append(str(component._purl))

    return component_purls
    

def purl_to_depgraph_dep(purl: str):
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