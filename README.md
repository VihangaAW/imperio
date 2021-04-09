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

#### Application Startup
Responsibilities of the code snippet in the application startup are given below.
* It will first check whether the offloading functionality is enabled by the Offload Manager App.
* If it is enabled, it will start looking for broadcast messages sent by the surrogate devices in the same network.
* Once a broadcast message is received, it check whether the sender is registered at the Offload Manager app. If so, it will establish a connection between the smartphone and the surrogate device.
* Lastly, it will invoke the Surrogate Profiler and Network Profiler which monitors surrogate device and the network conditions.
```java
[Documentation]

```

#### Places where you invoke on-device inference
Responsibilities of the code snippet you need to put in the places where on-device inference called are given below.
* It will invoke the Decision Making Engine, which deicdes when to offload.
* If the Decision Making Engine decides to execute the machine learning task locally, it will execute the task locally. Otherwise, it will offload the task to the connected surrogate device. Offloading funtionality is totally handled by the library.
```java
[Documentation]

```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.