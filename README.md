# How to run automated test

```
$ ./gradlew test
```

# Manual run
1. Visit [publisher website](https://1svkujd1fk.execute-api.us-east-2.amazonaws.com/). You will get uid here.
2. Click the ad, then it goes to [advertiser page](https://p64ya2x0wl.execute-api.us-east-2.amazonaws.com/)
3. Input item id and make a conversion. Make sure it sends a POST request to /conversion on publisher domain. 
4. Go to [publisher report](https://1svkujd1fk.execute-api.us-east-2.amazonaws.com/report) and make sure your uid and item id appears.

# How it works
## Pulisher website
It has 3 endpoints:
* landing page: GET /
* record conversion: POST /conversion
* reporting: GET /report

It sets `uid` cookie on landing page.   
Once conversion end point is hit, it gets item id from body and uid from `uid` cookie and put it in S3 file.  
Reporting page just shows the last n conversions by conversion timestamp.

## Advertiser website
It only has a landing page.  
It sends conversion request to publisher website everytime the button is clicked.
