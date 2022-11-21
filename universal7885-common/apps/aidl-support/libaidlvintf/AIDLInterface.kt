package com.royna.aidlvintf

object AIDLInterface {
     fun makeAIDLStr(name: String, kInterface: String, instance: String = "default") : String {
          var local = kInterface
          if (!kInterface.startsWith("I")) local = "I" + kInterface
          return name + "." + local + "/" + instance
     }
}
