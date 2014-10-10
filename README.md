# ajax-to-websocket

This server acts as a websockets proxy to any json api designed for ajax polling. The user makes a request for some urls and the server keeps polling the urls, only sending a websocket message when a new poll returns a different result from the last poll.

More documentation is forthcoming.

## Installation

Clone the repo.

## Usage

Run "lein deps" to download the dependencies.
Run "lein run" to run the server on localhost:8081.

Demo/demo.html should give you an idea of how to use it. :)

## Thanks

Thanks to Matthias Nehlsen for pointing out the mistake that caused me to spend way too much debugging. :)

## License

Copyright © 2014 Erik Ferguson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
