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
* landing page (GET /): generates and set `uid` cookie  
* record conversion (POST /conversion): take `uid` cookie and `item` in the body, and store as a conversion 
* reporting (GET /report): show the latest 10 conversions 

## Advertiser website
It only has a landing page.  
It sends conversion request to publisher website everytime the button is clicked.
