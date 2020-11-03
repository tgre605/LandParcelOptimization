# LandParcelOptimization

## How to run Land Parcel Optimizer via precompiled jar file

Ensure running Java 8 or newer

Open terminal and navigate to `\LandParcelOptimization\out` directory

Enter: `java -jar LandParcelOptimization.jar`

This will default to running the test (simple) variation of the city layout allowing for faster visualisation of each example district

To run larger model (make take upwards of ~25 mins) run `java -jar LandParcelOptimization.jar roadnetwork.json`
