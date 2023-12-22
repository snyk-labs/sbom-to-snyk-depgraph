![snyk-oss-category](https://github.com/snyk-labs/oss-images/blob/main/oss-example.jpg)

# sbom-to-snyk-depgraph
convert cyclone dx sbom to snyk depgraph and monitor/test/print

```
Usage: main.py [OPTIONS] COMMAND [ARGS]...

Options:
  --sbom-file TEXT      Full path to SBOM file  [env var: SBOM_FILE]
  --debug / --no-debug  Set log level to debug  [default: no-debug]
  --help                Show this message and exit.

Commands:
  monitor      Monitor SBOM with Snyk
  print-graph  Print Snyk depGraph representation of SBOM
  test         Test SBOM with Snyk
  ```

  # Setup

  Requires [Poetry](https://python-poetry.org/) for Python

  poetry install

  
  # Examples

  ### monitor sbom as snyk project
  ```
  Usage: main.py monitor [OPTIONS]

  Monitor SBOM with Snyk

Options:
  --snyk-token TEXT         Please specify your Snyk token  [env var:
                            SNYK_TOKEN]
  --snyk-org-id TEXT        Please specify the Snyk ORG ID to run commands
                            against  [env var: SNYK_ORG_ID]
  --snyk-project-name TEXT  Specify a custom Snyk project name. By default
                            Snyk will use the name of the root node.  [env
                            var: SNYK_PROJECT_NAME]
  --help                    Show this message and exit.
  ```

  poetry run python3 main.py --sbom-file="/path/to/bom.json" monitor --snyk-org-id=<org_id>

  ### test sbom against snyk database
  ```
  Usage: main.py test [OPTIONS]

  Test SBOM with Snyk

Options:
  --snyk-token TEXT   Please specify your Snyk token  [env var: SNYK_TOKEN]
  --snyk-org-id TEXT  Please specify the Snyk ORG ID to run commands against
                      [env var: SNYK_ORG_ID]
  --help              Show this message and exit.
  ```
  
  poetry run python3 main.py --sbom-file="/path/to/bom.json" test --snyk-org-id=<org_id>


