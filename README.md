# Document Managment Web API :
This API allows to save and retrieve documents very easly and fastly.

It works in a reactive way from the DB to the Browser : 
it means as soon as text is found in a the DB, it is displayed in the browser (it works with Flux). It doesn't wait to have the entire result before begining to display it in the browser.

## Functionalities :
- Save PDF, Microsoft Word (.doc), or text (.txt) documents in MongoDB
- Retrieve all documents containing a Specific word
- Retrieve all paragraphs in documents containing a specific word
- Open document
- list all documents name

# Implementation :
 
## With :
- Java 1.8 (Stream)
- Spring 5 (WebFlux), Spring Boot (2.0.0.M5), Spring MongoDB reactive
- Guava
- Apache PdfBox
- docx4j
- Swagger

## Tools :
- IntelliJ
- Maven
- GIT

## Persistence :
 - MongoDB <a href="https://mlab.com/welcome/" target="_blank">MLAB</a> (for data and files)
 
# How to run :

## Config :

### application.properties :
- server port :<br>
	server.port=8080
- context path :<br>
	server.servlet.context-path=/putwhatyouwantbutdontforgettheslash
- MongoDB ref :<br>
	spring.data.mongodb.uri=mongodb://geduser:xxxx@ds241065.mlab.com:41065/gedover
- <b>put application.properties in the dir from where you launch the app</b>
### Ex : application.properties
 			# MongoDB ref
 			spring.data.mongodb.uri=mongodb://geduser:xxxxx@ds241065.mlab.com:41065/gedover
 			# Size of the files which can be uploaded
			spring.servlet.multipart.max-file-size=50MB
			spring.servlet.multipart.max-request-size=150MB
			# server
			server.port=8080
			server.servlet.context-path=/

## Run :
### Build it (from the source in Github or Download the jar from my nexus) :
- <a href="http://182-193-28-81.ftth.cust.kwaoo.net:8081/nexus/content/repositories/snapshots/com/pat/ged/docmanager-web/1.0-SNAPSHOT/" target="_blank">Snapshot</a> ( last jar )
- <a href="http://182-193-28-81.ftth.cust.kwaoo.net:8081/nexus/content/repositories/releases/com/pat/ged/docmanager-web/1.0.RELEASE/docmanager-web-1.0.RELEASE.jar" target="_blank">Release</a>
### with jdk 1.8, launch (from the directory where you have downloaded the jar) :
		java -jar docmanager-web-1.0.RELEASE.jar

## Tests :	
	
### With Sawgger API :
- <a href="http://localhost:8080/swagger-ui.html" target="_blank">localhost:8080/swagger-ui.html</a>

### url : 
		
- return all paragraphs in doc containing {word} :<br>
	<a href="http://localhost:8080/api/paragraph/{word}" target="_blank">localhost:8080/api/paragraph/{word}</a>
- return all doc containing the  word ( with a list of lines in which is the word )<br>
	<a href="http://localhost:8080/api/file/{word}" target="_blank">localhost:8080/api/file/{word}</a>
- ( I let you see the API with swagger ...)

        note : {word} --> is the word to search	
# And finally, I let you create the Front-End part ...
(Becoze I'm Back-End developer ... )  <br>
<i>and if you do it, don't forget to tell me !</i><br>
<b>:-)</b>

	
	
