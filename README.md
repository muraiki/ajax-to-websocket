# ajax-to-websocket

This server acts as a websockets proxy to any json api designed for ajax polling, such as information for dashboards. Are you struggling while transitioning your Web 1.0 app into something more modern? Do you not want to deal with polling logic in the client, preferring to use websockets, but don't yet have the backend in place to support such a feature? This server could help!

Using this server, the client makes a request for some urls and the server keeps polling the urls, only sending a websocket message to the client when a new poll returns a different result from the last poll. The urls are retrieved pretty much in parallel (doseq with http-kit's async get) and results are returned via a core.async channel, so everything works concurrently... assuming I haven't messed something up as this is my first time using core.async!

Remember, though, this isn't a magic bullet: your server will still be getting hit by polls and will need to be able to deal with that load. But at least you can write your frontend in a more modern way, including eliminating polling logic, while hopefully working on getting your server achitecture updated at a later point in time. :)

## Demo

A demo file is included in demo/demo.html to illustrate how to use the server. It's not terribly pretty, but in it you can see the following:

```
var toSend = [
  {
    name: "jsontest",
    url: "http://ip.jsontest.com/"
  },
  {
    name: "datetime",
    url: "http://time.jsontest.com/"
  }
];
```

This is then converted to JSON and sent as a message on the websocket channel. The server will keep polling both ip.jsontest.com and time.jsontest.com. If you get a json parsing error in the server, it could be that jsontest.com is over quota and is returning an html response, which as of this time, it unfortunately is.

Since your computer's IP probably won't be changing, you'll see in your browser window that the IP is only displayed once; your client will only ever receive one websocket message regarding this url.

However, since the time is of course continually changing, your browser window will keep updating the time as it receives a new websocket message with each poll.

## Installation

Clone the repo.

Run "lein deps" to download the dependencies.

## Usage

Run "lein run" to run the server on localhost:8081.

Establish a websocket connection to the server.

Send, as JSON, an array of objects where each has the following two keys:

* name: Since the server can poll a number of urls, each message the client receives will include this name for the purpose of differentiating between responses.

* url: The url to poll. Currently, only GET is supported as a method.

## To Do

I constructed this as a proof of concept for a problem I ran into at my job. That being said, I'd like to add some more features. Pull requests are welcome. :)

* A less generic name
* Support for other HTTP methods
* Set timeouts independently per url.
* Set up a filter so that only certain URLs will be accepted (so a user can't start polling Google)
* Set a limit on the maximum number of URLs, to prevent use for DDOS.

## Thanks

Thanks to [Matthias Nehlsen](http://matthiasnehlsen.com) for pointing out the mistake that caused me to spend way too much debugging. :) He has an excellent blog on Clojure that's worth checking out!

## License

Copyright © 2014 Erik Ferguson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
