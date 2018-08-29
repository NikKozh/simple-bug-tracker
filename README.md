# simple-bug-tracker
The simpliest bug tracker with adding and deleting tasks. Scala + Play Framework, using PostgreSQL and Slick as ORM. Apache Lucene for full-text search.

The application was deployed on Clever Cloud: http://app-3ec2dec5-6155-4cc1-ac1a-431c4edfb1c0.cleverapps.io/

To use it locally you need:
* PostgreSQL Server
  * address: localhost 
  * user: "postgres" 
  * password "1234"
  * empty database: bugtracker_db
* Execute "sbt.bat" in the root directory and use "run" command.

The server will be on http://localhost:9000 address.
