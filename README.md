# simple-bug-tracker
The simpliest bug tracker with adding and deleting tasks. Scala + Play Framework, using PostgreSQL and Slick as ORM. Apache Lucene for full-text search.

The application was deployed on Heroku: https://simple-bug-tracker.herokuapp.com/

Note that frontend is made very badly (non-adaptive and even not cross-browser) since I'm not a front-end developer at all and my purpose was to make the good backend.

To use it locally you need:
* PostgreSQL Server
  * address: localhost 
  * user: "postgres" 
  * password "1234"
  * empty database: bugtracker_db
* Execute "sbt.bat" in the root directory and use "run" command.

The server will be on http://localhost:9000 address.
