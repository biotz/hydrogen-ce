# Hydrogen Community Edition #

# About #
Hydrogen is a full-stack platform based on the [Duct framework](https://github.com/duct-framework/duct). This repository contains a
demo of a SPA using Hydrogen Community Edition that implements the following features:

* **Frontend** Reagent is used for the user interface, and re-frame to manage the application state. 
[re-frame-10x](https://github.com/Day8/re-frame-10x) plugin is also installed, and can be used to inspect the application state and the event flow in real time.
To see the usability of the plugin, a playground (an interactive TODO list) is included.
* **Routing:** [Secretary](https://github.com/clj-commons/secretary) is used for the client side routing. The demo also implements routing logic based on the user authentication.
* **Token based authentication:** 
	* **Token retrieval** AWS Cognito is the chosen OpenId provider for this demo.
	* **Token validation** the retrieved user tokens are validated using the module [buddy-auth.jwt-oidc](https://github.com/magnetcoop/buddy-auth.jwt-oidc) 
* **Encryption:** The demo has a simple example of text encryption and decryption using the [encryption](https://github.com/magnetcoop/encryption) module. 
* **Secret storage:** User's privates keys used for the encryption example are stored in AWS SSM Parameter Store using the [secrets-storage.aws-ssm-ps](https://github.com/magnetcoop/secret-storage.aws-ssm-ps) module.

# Modules #
Apart from Duct core modules, Hydrogen is composed of multiple libraries/modules developed by Magnet:

* [**Buddy-auth.jwt-oidc**](https://github.com/magnetcoop/buddy-auth.jwt-oidc): Duct library that provides jwt-token validation.
* [**Encryption**](https://github.com/magnetcoop/encryption): Library for encrypting /decrypting arbitrary values.
* [**Secret storage**](https://github.com/magnetcoop/secret-storage.aws-ssm-ps): Duct library for storing secrets in AWS.

The following modules don't have any specific functionality related with the demo, but their configuration is present as an example:

* [**Scheduling Twarc**](https://github.com/magnetcoop/scheduling.twarc): Duct library for using Twarc with PersistentJobs.
* [**Object storage**](https://github.com/magnetcoop/object-storage.s3): Duct library for managing AWS S3 objects. 


For more information about the usage of every module please refer to the links above.

# Usage #
To run this application locally please make sure you meet the following setup:

### Requisites
* Leiningen 2.8.3 or later
* Postgres database
* AWS Account
	* Cognito user pool configured
	* Credentials for accessing the needed resources (SSM and S3)
### Environment variables
* Database: `POSTGRES_DB` `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
* Authentication: `OIDC_ISSUER_URL`, `OIDC_AUDIENCE`, `OIDC_JWKS_URI`
* Secrets storage: `SSM_SP_AWS_KMS_KEY`, `SSM_SP_USERS_KEY_PATH`
* Object storage: `OBJECTS_STORAGE_S3_BUCKET`
* AWS: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESs_KEY`, `AWS_DEFAULT_REGION`

### Running the application
#### Development
To begin developing, start the REPL.

```sh
lein repl
```

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to prep and initiate the system.

```clojure
dev=> (go)
:duct.server.http.jetty/starting-server {:port 3000}
:initiated
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```
#### Uberjar
To run the application from an uberjar, make sure you add the `:duct/migrator` key to the launching arguments in order to execute the migrations required by the [scheduling module](https://github.com/magnetcoop/scheduling.twarc).

`lein uberjar`


`java -jar hyd-standalone.jar :duct/migrator :duct/daemon`

# License #

Copyright (c) Magnet S Coop 2018.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.