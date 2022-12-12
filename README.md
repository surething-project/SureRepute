<p align="center">
  <img src="./sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">SureRepute: User Reputation in Location Certification Systems</i></h3>

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

## Publication

Figueiredo, R.; Eisa, S. & Pardal, M. L.  
_SureRepute: Reputation System for Crowdsourced Location Witnesses_  
21st IEEE International Symposium on Network Computing and Applications (NCA), 2022  

```bibtext
@InProceedings{Figueiredo_2022_NCA_SureRepute,
  author    = {Rafael Figueiredo and Samih Eisa and Miguel L. Pardal},
  booktitle = {21st IEEE International Symposium on Network Computing and Applications (NCA)},
  title     = {{SureRepute: Reputation System for Crowdsourced Location Witnesses}},
  year      = {2022},
  month     = dec,
  abstract  = {Location is an important attribute for many mobile applications but it needs to be verified. For example, a user of a tourism application that gives out rewards can falsify his location to pretend that he has visited many attractions and thus receive benefits without deserving them. To counter these attacks, the system asks users to prove their location through witnesses, i.e., other devices that happen to be at the location at the same time and that can be partially trusted. However, for this approach to be effective, it is important to keep track of the witness behavior over time. Many crowdsourcing applications, like Waze, build up reputations for their users, and rely on user co-location and redundant inputs for data verification.
In this work, we present SureRepute, a reputation system capable of withstanding reputation attacks while still maintaining user privacy. The results show that the system is able to protect itself and its configuration is flexible, allowing different trade-offs between security and usability, as required in realworld applications. The experiments show how the reputation system can be easily integrated into existing applications without producing a significant overhead in response times.},
  keywords  = {Reputation System, Location Certification Systems, Privacy, Reputation Attacks, Defense Against Reputation Attacks, Reputation Score},
}
```
