Generated base project from gitter template https://github.com/http4s/http4s.g8

### Usage
1. run the server using `sbt run`
2. send a request to localhost:8080 containing the lattitude and longitude respectively. ex:

`curl -i http://localhost:8080/39.7456/-97.0892`

### General thoughts
This is definitely a proof of concept level of effort. There are things I would improve given more time. In no particular order they are:
* Add unit tests, and maybe even some integration tests
* I used a lot of `.toOption.get` which is not best practice, so I would add ways to fail gracefully for all of those using `EitherT`
* Currently, the endpoint wont match if there are not two `Double`s, but you might want to fail more directly with a `BadRequest` instead if the user puts in a string or something to give the user better understanding of what is happening.
* I'm adding the headers in the service itself and if this app is intended to only use this api then it might make more sense to have it be middleware at the server level.
* Currently the service definition, implementation, and case classes are all in one file, but if I were to add functionality then I would probably separate at least the case classes.
* I'm assuming that the first result in the json array for the forcast call will always be the one I want, but order in json arrays is not necessarily guaranteed, so it would be better to check using the provided `number` json attribute
* The weatherservice implementation requires `Async` because it's the most powerful of the type conditions and will satisfy basically any need, and I just wanted it to run, but it's better practice to use the least powerful that you can get away with.