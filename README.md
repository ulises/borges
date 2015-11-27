# borges

Borges is an erlang binary term encoder/decoder.

[![Build Status](https://travis-ci.org/ulises/borges.png?branch=master)](https://travis-ci.org/ulises/borges)

## Including borges in your project

Both the encoder and decoder namespaces provide a main function used
for encoding and decoding.

Include `[borges "0.1.6"]` in your project.clj if you're using leiningen. Alternatively, add

````
    <dependency>
        <groupId>borges</groupId>
        <artifactId>borges</artifactId>
        <version>0.1.6</version>
    </dependency>
````

if you're using maven.

# Using borges

Hopefully this is as straightforward as:

````
    user> (require '[borges.encoder :refer [encode])
    nil
    user> (def encoded (encode [1 2 3]))
    #<HeapByteBuffer java.nio.HeapByteBuffer[pos=0 lim=18 cap=65535]>
    user> (def encoded (encode [1 2 3]))
    #'user/encoded
    user> encoded
    #<HeapByteBuffer java.nio.HeapByteBuffer[pos=0 lim=18 cap=65535]>
    user> (require '[borges.decoder :refer [decode]])
    nil
    user> (decode encoded)
    [1 2 3]
````

# Encoding/decoding

Borges can encode/decode ~all data structures listed in the
[erlang external term format][].

# Developing

Please fork and send pull requests my way.

## Running the tests

`lein test` should do it.

# License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

````
    http://www.apache.org/licenses/LICENSE-2.0
````

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[erlang external term format]: http://erlang.org/doc/apps/erts/erl_ext_dist.html
