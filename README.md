![snyk-oss-category](https://github.com/snyk-labs/oss-images/blob/main/oss-example.jpg)

# sbom-to-snyk-depgraph
Convert [CycloneDX](https://cyclonedx.org/) SBOM to snyk depgraph and monitor/test/print

```
Usage: main.py [OPTIONS] COMMAND [ARGS]...

Options:
  --sbom-file TEXT                Full path to SBOM file  [env var: SBOM_FILE;
                                  required]
  --prune-repeated-subdependencies / --no-prune-repeated-subdependencies
                                  Use if too many repeated sub dependencies
                                  causes test or monitor to fail  [default:
                                  no-prune-repeated-subdependencies]
  --debug / --no-debug            Set log level to debug  [default: no-debug]
  --help                          Show this message and exit.

Commands:
  monitor      Monitor SBOM with Snyk
  print-graph  Print Snyk depGraph representation of SBOM
  test         Test SBOM with Snyk
  ```

  ### Exit Codes

  - 2 = an error occurred during execution and did not complete successfully (see error message on stdout)

  When using `test` command
  - 0 = test completed successfully and no security issues were found
  - 1 = test completed successfully and 1 more security issues were found

  When using other commands (e.g. `monitor` or `print-graph`)
  - 0 = command completed successfully


  # Setup

  Requires [Poetry](https://python-poetry.org/) for Python

  poetry install

  
  # Examples

  ### Monitor SBOM as Snyk project
  ```
  Usage: main.py monitor [OPTIONS]

  Monitor SBOM with Snyk

Options:
  --snyk-token TEXT         Please specify your Snyk token  [env var:
                            SNYK_TOKEN]
  --snyk-org-id TEXT        Please specify the Snyk ORG ID to run commands
                            against  [env var: SNYK_ORG_ID]
  --help                    Show this message and exit.
  ```

  poetry run python3 main.py --sbom-file="/path/to/bom.json" monitor --snyk-org-id=<org_id>

  ### Test SBOM against Snyk database
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

