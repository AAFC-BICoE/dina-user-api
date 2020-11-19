# user-api

AAFC DINA user module implementation.

## Authentication

Any regular DINA token can be used to authenticate with the DINA User service. Internally, it uses the "user-svc" Keycloak resource to login to a dedicated service account which can perform user management tasks. User-, gorup- and role-based restrictions will be implemented in the User Service code to ensure that only authorized changes can be made.

For the service account to work, it needs the clientId (user-svc) as well as the client secret. In dev, the secret is a fixed value defined in the keycloak-starter-realm json file and reflected in the `KEYCLOAK_USER_SVC_SECRET` environment variable. In production, the secret should be changed in the Keycloak deployment (Clients -> user-svc -> Credentials -> Regenerate Secret) and the environment variable updated to match.
