# 2. Sandbox

Date: 2019-04-29

## Status

Accepted

## Context
There is need to implement a sandbox environment that replicates the minimal functionality required to accurately test APIs during development and integration.
However if the sandbox environment is isolated from the production environment and database, Developer Experience can be negatively affected because any sandbox or UAT downstream service will have significantly more downtime than its corresponding production service.
It is therefore necessary to implement a solution that maximizes uptime of both sandbox and production environments.

## Decision
Both live and sandbox environments of a service should be routed to the same downstream production service.
Test credentials should be created with a "test" tag in order to differentiate between test and live credentials.

API Gateway should have the ability to determine credential type (test or live) from a decoded JSON Web Token.

API Gateway should have a filter that dynamically forwards or rejects requests to downstream production services depending on whether the request source matches credential type. This means,
requests made from a sandbox environment must use test credentials and requests made from live environment must use live credentials, this will ensure there are no
mistakes or unnecessary calls to the downstream production service.
.

## Consequences
By routing both the sandbox and live URLs to the same production service, we would ensure maximum uptime for both live and sandbox environments and eliminate the need for a dedicated sandbox service. By using the same database, we would handle scenarios where data needs sync between live and sandbox environments.





