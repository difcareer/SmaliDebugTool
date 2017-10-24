# SmaliDebugTool

#关于本项目
  本项目可以从一个apk文件，生成对应的可调试的smali工程

#解决了哪些问题
1. 一键式处理
2. multidex 的处理
3. 文件合并

#获取
1. 你可以从这里下载https://github.com/difcareer/SmaliDebugTool/releases
2. 也可以clone项目，bin目录下包含可执行jar

#使用方式
1. 进入bin目录
1. 配置好smaliidea插件，参见：[smalidea](https://github.com/JesusFreke/smali/wiki/smalidea)
3. ```java -jar SmaliDebugTool.jar path/of/apk path/of/output```

#Jre版本

本项目使用了jdk7里面的一些语法糖，故再次编译，或者直接使用时，jre版本至少为1.7
