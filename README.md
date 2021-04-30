# Imperio

Imperio is a Android library to offload machine learning related tasks on Andorid mobile application developed using TensorFlow Lite.

#### Supported Platforms and Libraries
Currently Imperio library only supports Android applications and machine learning models which can inference using TensorFlow Lite Task Library.

## Installation

In order to use this library, you have to import this Android library to your Android project.

### Clone this repository
```bash
git clone https://github.com/VihangaAW/imperio.git
```

### Import Imperio module to your Android project
In Android Studio, go to File -> New -> Import Module
In the "Import Module from Source" pop up, select the path of the downloaded module and click Finish
Now you have to build the project, to do that, go to Build -> Make Project

### Import module to your working module as a dependancy
Go to File -> Project Structure -> Click "Dependancies" tab 

In the dependancies tab, select your working module and click + button in the "Declared Dependandies"

You will get a list when you click on + button, in the list, select "Module Dependancy"

![alt text](https://i.imgur.com/MqXMGyZ.jpg)

In the pop up box, select Imperio module and click ok

![alt text](https://i.imgur.com/Gx6trFa.jpg)

Thats it!


## Usage

You only need to put the following code into two places. You need to put the code in,

* Application startup

* Places where you invoke on-device inference

  Following documentation using BertNLClassifier API of the TensorFlow Lite Task Libarary as an example.

#### Application Startup

Responsibilities of the code snippet in the application startup are given below.

* It will first check whether the offloading functionality is enabled by the Offload Manager App.

* If it is enabled, it will start looking for broadcast messages sent by the surrogate devices in the same network.

* Once a broadcast message is received, it check whether the sender is registered at the Offload Manager app. If so, it will establish a connection between the smartphone and the surrogate device.

* Lastly, it will invoke the Surrogate Profiler and Network Profiler which monitors surrogate device and the network conditions.

```java
//Variables to Declare/Initialize
private  static  final  String  INFERENCE_API = { Name of the TensorFlow Lite Task Library API used }; //Example: "BertNLCLassifier"
private  String  surrogateIpAddress;
private  int  temp = 0;
private  int  executeCount = 1;
private  int  initialInferenceDelay = 0;
private  DecisionMaker  decisionMaker;
private  ImperioSQLiteDBHelper  imperioSQLiteDBHelper;
// Following variables contains the names of your machine learning task.
// Each name should be unique and each name should have a seperate variable
private  String  task_id1 = { Unique name for the machine learning task } //Example: "TextClassificationClient1";
// Initiate the classifier and the variable used to store the outputs. You can use the 
// same variables used for the normal execution
// Example: BertNLClassifier  classifier;
//          List<Category> apiResults = new  ArrayList<Category>();

// Place the following code inside your class's constructer
this.imperioSQLiteDBHelper = new  ImperioSQLiteDBHelper(this.context);
// Each task should have a seperate DecisionMaker object
this.decisionMaker = new  DecisionMaker(this.context, task_id1);

// Place the following code where you initialize the classifier for normal execution
// Following method is used to initialize broadcast receiver service, surrogate 
// and network profilers and the connection initializer
Thread  threadReceiveBroadcast = new  Thread(new  Runnable() {
	@Override
	public  void  run() {
		try {
		   InvokeConnector  ic = new  InvokeConnector(context, MODEL_PATH, INFERENCE_API);
		   ic.start();
		   if(ic.getOffloadEnabled()){
		      while(ic.getSurrogateIpAddress()==null){
			  }
			surrogateIpAddress = ic.getSurrogateIpAddress();
		  }
		  else{
		     surrogateIpAddress = null;
		  } 
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
});
threadReceiveBroadcast.start();
```

  

#### Places where you invoke on-device inference

Responsibilities of the code snippet you need to put in the places where on-device inference called are given below.

* It will invoke the Decision Making Engine, which deicdes when to offload.

* If the Decision Making Engine decides to execute the machine learning task locally, it will execute the task locally. Otherwise, it will offload the task to the connected surrogate device. Offloading funtionality is totally handled by the library.

```java

// Place the following code inside your classification method. It will invoke
// the Decision Making Engine and performs either local execution or remote execution
if(decisionMaker.makeDecision()==0){
	taskLocallyExecute(text);
}
else{
	taskOffload(text);
}

// You need to create two new methods called taskLocallyExecute and taskOffload.

// Method      : taskLocallyExecute
// Parameters  : input text (string)
// Return Value: No Return Value (Void)
// Place your code related to local execution inside a new method calles taskLocallyExecute
public  void  taskLocallyExecute(String  text) throws  Exception {
	Instant  localExecuteStart = Instant.now();
	// Run inference. 
	////////////////////////////////////////////////////
	// Place your normal execution code here
	// Example: apiResults = classifier.classify(text);
	///////////////////////////////////////////////////
	Instant  localExecuteEnd = Instant.now();
	Duration  localTimeElapsed = Duration.between(localExecuteStart, localExecuteEnd);
	imperioSQLiteDBHelper.AddExecutionTimeLocal(task_id1,localTimeElapsed.toMillis(), decisionMaker.getLocalMAD());
}

// Method      : taskOffload
// Parameters  : input text (string)
// Return Value: No Return Value (Void)

public  void  taskOffload(String  text) throws  Exception {
	if(surrogateIpAddress!=null){
	    /////////////////////////////////////////////////////////////////
	    // Place a suitable output variable here
		// Example: float[][] output = new  float[1][2];
		/////////////////////////////////////////////////////////////////
		Instant  offloadStart = Instant.now();
		//task offload
		JSONObject resultJson = OffloadManager.offload((long) decisionMaker.getLocalTimeExecute(),text);
		/////////////////////////////////////////////////////////////////
		// Place the code related to assign the results to variable you used to store the output
		// Example: Category catNegative = new Category("negative",Float.parseFloat(resultJson.getString("negative")));
		//          Category catPositive = new Category("positive",Float.parseFloat(resultJson.getString("positive")));
		//          Since apiResults variable is a class variable, we need to clear all
		//          the previous data in the list before assign new values
		//          apiResults.clear();
		//          apiResults.add(catNegative);
		//          apiResults.add(catPositive);
		/////////////////////////////////////////////////////////////////
		Instant  offloadEnd = Instant.now();
		Duration  offloadTimeElapsed = Duration.between(offloadStart, offloadEnd);
		if(OffloadManager.isTaskTimeOut() || OffloadManager.isTaskHasErrors()){
			//Execute locally
			taskLocallyExecute(text);
		}
		else{
		    imperioSQLiteDBHelper.AddExecutionTimeOffload(task_id1,offloadTimeElapsed.toMillis(), decisionMaker.getOffloadMAD());
		}
	}
	else{
		taskLocallyExecute(text);
	}
}
```
Thats it!

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.