# user-api

AAFC DINA user module implementation.

The user module is used to simplify access to some information stored in Keycloak and additional information like user preferences.

## To Run

The easiest way to run the user module is to use the [dina-local-deployment](https://github.com/AAFC-BICoE/dina-local-deployment).

## Authentication

Any regular DINA token can be used to authenticate with the DINA User service. Internally, it uses the "user-svc" Keycloak resource to login to a dedicated service account which can perform user management tasks. User-, group- and role-based restrictions are implemented in the User Service code to ensure that only authorized changes can be made.

For the service account to work, it needs the clientId (user-svc) as well as the client secret. In dev, the secret is a fixed value defined in the keycloak-starter-realm json file and reflected in the `KEYCLOAK_USER_SVC_SECRET` environment variable. In production, the secret should be changed in the Keycloak deployment (Clients -> user-svc -> Credentials -> Regenerate Secret) and the environment variable updated to match.

