# api-java-getting-started

Java exampe for Elit's API:

# Clone and Run Locally 

Install the Heroku CLI
https://devcenter.heroku.com/articles/heroku-cli

$ git clone git@github.com:becwatson/api-java-getting-started.git # or clone your own fork

$ cd api-java-getting-started

Using the webapp runner we can run the code locally as outlined in this guide the webapp runner is
used by Heroku to deploy the webapp as well:
https://devcenter.heroku.com/articles/java-webapp-runner

$ mvn package

$ java -jar target/dependency/webapp-runner.jar target/*.war

# Deploy to Heroku

$ heroku create

$ git push heroku master

$ heroku open

# Updating App

Update the code files, commit changes to your local git clone then push local changes to heroku:

$ git commit -a -m "Updated files"

$ git push heroku master

$ heroku open


# API access

Apply for API key via wiapi@elit and set environment variables.


For the locally running test app:

$ export WI_ACCOUNT_ID="<account_id>"

$ export WI_ACCOUNT_TOKEN="<account_token>"


For heroku this can be done as follows:

$ heroku config:set WI_ACCOUNT_ID="<account_id>"

$ heroku config:set WI_ACCOUNT_TOKEN="<account_token>"

To unset variables use:

$ heroku config:unset WI_ACCOUNT_ID

$ heroku config:unset WI_ACCOUNT_TOKEN

# Documentation

http://docs.englishlanguageitutoring.com/
