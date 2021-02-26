# About
This repository is to reproduce the issue that third party cookies on the Safari 14 are sent even though it's supposed to be blocked by ITP.  
It happens only when it's launched by Selenium and safaridriver.  
Please try following manual and automated tests and see the difference.  

## Manual test
Make sure you're using **Safari 14**. 
1. Visit [publisher website](https://1svkujd1fk.execute-api.us-east-2.amazonaws.com/). You will get uid here.
2. Click the ad, then it goes to [advertiser page](https://hir0shim.github.io/fake-advertiser/index.html)
3. Input item id and make a conversion. Make sure it sends a POST request to /conversion on publisher domain. 
4. Go to [publisher report](https://1svkujd1fk.execute-api.us-east-2.amazonaws.com/report) and make sure your uid does not appear, which means third party cookies are **NOT** sent. 

## Automated test
It will simulate the same behavior as manual test above, and the test will pass if it the uid appears which means third party cookies are sent.  
**The test passed which means it actually sent third party cookies. This is the unexpected behavior**

### Automated test with local browser
Make sure safari driver installed and enabled.
Check out [Testing with WebDriver in Safari](https://developer.apple.com/documentation/webkit/testing_with_webdriver_in_safari) for details.
```
$ safaridriver --enable
```


Then run the test:
```
$ ./gradlew clean test -Ddriver=local
```

If you want to try with other browsers, please update `ThirdPartyCookieTest.setupLocalWebDriver()`.

### (Optional) Automated test with SauceLabs
If you want to use SauceLabs instead, follow the steps below.  
Get your credentials in Sauce Labs account page and set them to env vars:
```
export SAUCE_USERNAME="{YOUR SAUCE USER NAME}"
export SAUCE_ACCESS_KEY="{YOUR SAUCE ACCESS KEY}"
```

Then run the test:

```
$ ./gradlew clean test -Ddriver=remote
```

Update `ThirdPartyCookieTest.setupRemoteWebDriver()` to try with different browsers.  



# How the websites work
We have two websites on different domains to better check the cross domain behaviors.
## Pulisher website
It has 3 endpoints:
* landing page: GET /
* record conversion: POST /conversion
* reporting: GET /report

It sets `uid` cookie on landing page.   
Once conversion end point is hit, it gets item id from body and uid from `uid` cookie and put it in S3 file.  
Reporting page just shows the last 10 conversions by conversion timestamp.

## Advertiser website
It only has a landing page.  
It sends conversion request to publisher website everytime the button is clicked.
