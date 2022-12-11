<p align="center">
  <img src="./../sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">SureRepute-Modules</i></h3>
--

## Structure

| Directory                              |       Description        |
|:---------------------------------------|:------------------------:|
| [CA](CA)                               |        CA Module         |
| [IdentityProvider](IdentityProvider)   | Identity Provider Module |
| [SureRepute-Server](SureRepute-Server) | SureRepute-Server Module |
| [SureRepute-Client](SureRepute-Client) | SureRepute-Client Module |
| [Demo-Client](Demo-Client)             |    Demo-Client Module    |

## Script Guide
In the root directory exists a `setupServerUrls.sh` that based sets up the urls of a specific number of servers based on the data that is present on `data`.json
For example to set up 2 servers you do:
```shell script
./setupServerUrls.sh 2
```

`data.json` is a json based file that by default contains:
```json
{
   "ca": "https://localhost:9090",
   "identityProvider": "https://localhost:9091",
   "servers":[
      [
         "SureReputeServer1",
         "https://localhost:9092",
         "https://localhost:9093",
         "https://localhost:9094"
      ],
      [
         "SureReputeServer2",
         "https://localhost:9095",
         "https://localhost:9096",
         "https://localhost:9097"
      ]
   ]
}
```
The keys-Values pairs are:
- `CA`: Url where it is going to run the Certificate Authority
- `IdentityProvider`: Url where it is going to run the Identity Provider
- `Servers`: List where each index contains a list with the needed information for running each Server. For each server you can set up the identifier, the url for client communication, the url for server communication and the url for identity provider communication respectfully.

By default, 2 servers are set up, but you can set up more.

## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                                                       |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rafael Figueiredo | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo")                               |
