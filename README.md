[![MIT License][license-badge]][LICENSE]

# Demo Derby

> Smash em mash em, relieve stress! Live multi player demo derby, open app, add car, smash other folks!
> 
> App is being built as a fun way to blow off steam during the day. 
>
> In progress, so feel free to contribute!
   
## Used Versions

* [Play Framework: 2.6.9](https://www.playframework.com/documentation/2.6.x/Home)
* [React: 16.2.0](https://reactjs.org/)
* [Create React App: 1.0.17](https://github.com/facebookincubator/create-react-app)

## How to use it? 

### Prerequisites

* This assumes that you have [npm](https://npmjs.org/) installed.

### Let's get started,

* Clone the application and open application as a sbt project.

* This application is not using any of the java play views and all the views are served by the [React](https://reactjs.org/) code base which is inside the `ui` folder.

* Used any of the sbt commands listed in the below according to the requirement which are working fine with this application.(To see more details of [sbt](http://www.scala-sbt.org/))

``` 
    sbt clean           # Clear existing build files
    
    sbt stage           # Build your application from your project’s source directory
    
    sbt run             # Run both backend and frontend builds in watch mode
    
    sbt dist            # Build both backend and frontend sources into a single distribution
    
    sbt test            # Run both backend and frontend unit tests 
```
