# user-api

AAFC DINA user module implementation.


## To Run

For testing purpose a [Docker Compose](https://docs.docker.com/compose/) example file is available in the `local` folder.
Please note that the app requires Keycloak.

Create a new docker-compose.yml file and .env file from the example file in the local directory:

```
cp local/docker-compose.yml.example docker-compose.yml
cp local/docker-compose-keycloak.yml.example docker-compose-keycloak.yml
cp local/*.env .
```

Start the app (default port is 8083):

```
docker-compose --env-file network.env -f docker-compose.yml -f docker-compose-keycloak.yml up
```

Once the services have started you can access endpoints like "group" at http://localhost:8083/api/v1/group .
Note that this will run behind Keycloak so, you will need a token (see [dev-tool](https://github.com/AAFC-BICoE/dina-local-deployment/tree/master/dev-tools)).

Cleanup:
```
docker-compose --env-file network.env -f docker-compose.yml -f docker-compose-keycloak.yml down
```

## Authentication

Any regular DINA token can be used to authenticate with the DINA User service. Internally, it uses the "user-svc" Keycloak resource to login to a dedicated service account which can perform user management tasks. User-, gorup- and role-based restrictions will be implemented in the User Service code to ensure that only authorized changes can be made.

For the service account to work, it needs the clientId (user-svc) as well as the client secret. In dev, the secret is a fixed value defined in the keycloak-starter-realm json file and reflected in the `KEYCLOAK_USER_SVC_SECRET` environment variable. In production, the secret should be changed in the Keycloak deployment (Clients -> user-svc -> Credentials -> Regenerate Secret) and the environment variable updated to match.
