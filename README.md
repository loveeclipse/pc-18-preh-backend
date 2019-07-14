# Pre-Hospital
## A Pervasive Computing Project

https://www.unibo.it/en/teaching/course-unit-catalogue/course-unit/2018/412647

## Use Requirements

In order to use the system all that is needed is the Android application, found at the following link:
https://github.com/loveeclipse/pc-18-preh-frontend/releases

The services found on this repository are normally uploaded on Heroku, but in the eventuality that they aren't active a manual launch of all four services is required:

```
	java -jar pc-18-preh-backend-1.0-discovery-service.jar
	java -jar pc-18-preh-backend-1.0-events.jar
	java -jar pc-18-preh-backend-1.0-missions.jar
  	java -jar pc-18-preh-backend-1.0-patients.jar
```

In order to run the service jars, a version of Java equal or higher than 1.8 is required.
Note: run the discovery service **before** the others.

## Releases and Project Report
It is possible to download the source code and all release executable jars, along with the project report at the following page :
https://github.com/loveeclipse/pc-18-preh-backend/releases  

## API Documentation
All the API documentation, concenring the available REST calls can be viewed at the following link:   
https://app.swaggerhub.com/apis-docs/candoz/pre-hospital/1


## Team members
Nicola Atti (nicola.atti@studio.unibo.it)              
Marco Canducci (marco.canducci@studio.unibo.it)       
Chiara Volonnino (chiara.volonnino@studio.unibo.it)       
Daniele Schiavi (daniele.schiavi@studio.unibo.it)         
