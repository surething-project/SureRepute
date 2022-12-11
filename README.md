<p align="center">
  <img src="./sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">SureRepute: User Reputation in Location Certification Systems</i></h3>
---

## Structure

| Directory            |                           Description                           |
|:---------------------|:---------------------------------------------------------------:|
| [Modules](Modules)   |                       SureRepute Modules                        |
| [Contract](Contract) |                 SureRepute Contract Definitions                 |

## Scripts

- `initialSetup.sh`: Script that builds all contracts, sets up the CA certificate as trusted, and creates local
  databases for the identity provider and the nº of servers requested
    - Note: Servers are dependent on the information given in [data.json](Modules/data.json),
      see [modules' readme](Modules/README.md) for more details)
- `run.sh`: Runs the entire project locally. It runs the CA, Identity Provider and the nº of servers requested.
    - Note1: You need to have the databases created and the CA certificate setup as trusted. This can be done by
      running `initialSetup.sh` or by manually doing so (See specific readmes for each module)
    - Note2: Servers are dependent on the information given in [data.json](Modules/data.json),
      see [modules' readme](Modules/README.md) for more details, and in order to run you need to)

## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                                                       |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rafael Figueiredo | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo")                               |
