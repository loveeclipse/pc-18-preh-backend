# Pre-Hospital
## A Pervasive Computing Project

https://www.unibo.it/en/teaching/course-unit-catalogue/course-unit/2018/412647

## Use Requirements

In order to use the system all that is needed is the Android application, found at the following link:  
https://github.com/loveeclipse/pc-18-preh-frontend/releases  

The services found on this repository are designed to be uploaded on Heroku, and must be activated in order for the system to work.
The services can also be run locally, executed by default on *localhost* on the following ports: 

```
  discovery-service on port 5150 
  events on port 10000
  missions on port 10001
  patients on port 10002
```

Before running them, the environment variable **MONGO_CONNECTION_STRING** must be added with the following format:  
_mongodb://{user}:{password}@{host}:{port}/{database}_

If deployed on heroku you must add the following environment variables for each service:
- **{SERVICE_NAME}_HOST**: host of the internal network (use 0.0.0.0 for heroku)
- **{SERVICE_NAME}_EXTERNAL_HOST**: public host of the server (e.g. pc-18-preh-discovery.herokuapp.com)

Except the discovery service they must also add the environment variables host and port of the discovery service:
- **DISCOVERY_HOST** (e.g. pc-18-preh-discovery.herokuapp.com)
- **DISCOVERY_PORT** (e.g. 443 for https)

In order to run these services their jars are found in the release tab.
```
  java -jar pc-18-preh-backend-1.0-discovery-service.jar
  java -jar pc-18-preh-backend-1.0-events.jar
  java -jar pc-18-preh-backend-1.0-missions.jar
  java -jar pc-18-preh-backend-1.0-patients.jar  
```

Note: run the discovery service **before** the others.

## Releases and Project Report
It is possible to download the source code and all release executable jars, along with the project report
at the following page:  
https://github.com/loveeclipse/pc-18-preh-backend/releases  

## API Documentation
All the API documentation, concenring the available REST calls can be viewed at the following link:  
https://app.swaggerhub.com/apis-docs/candoz/pre-hospital/1  


## Team members
Nicola Atti (nicola.atti@studio.unibo.it)  
Marco Canducci (marco.canducci@studio.unibo.it)  
Chiara Volonnino (chiara.volonnino@studio.unibo.it)  
Daniele Schiavi (daniele.schiavi@studio.unibo.it)         
