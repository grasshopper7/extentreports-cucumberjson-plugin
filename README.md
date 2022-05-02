This deals with **generating Extent reports for Cucumber-JVM**, version 4.3 and above, by using a **Maven Plugin which parses the Cucumber JSON report**. For more details refer to this [article](https://ghchirp.tech/2114/). Sample implementation can be found [here](https://github.com/grasshopper7/extentreports-cucumberjson-report).

The **plugin version 2.0.0** uses **ExtentReport version 5** which deprecates many reporters other than **Spark, Json and Klov**. This plugin also includes a **custom [Pdf reporter](https://github.com/grasshopper7/cucumber-pdf-report)**. A ported version of the HTML extent report is also generated. The Spark, Json, Pdf and Html reports are enabled by default. Use the below **configuration** for the plugin.


```
<plugin>
	<groupId>tech.grasshopper</groupId>
	<artifactId>extentreports-cucumberjson-plugin</artifactId>
	<version>2.11.4</version>
	<executions>
		<execution>
			<id>report</id>
			<phase>post-integration-test</phase>
			<goals>
				<goal>extentreport</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<cucumberJsonReportDirectory>${project.build.directory}</cucumberJsonReportDirectory>
		<extentPropertiesDirectory>${project.build.testResources[0].directory}</extentPropertiesDirectory>
		<displayAllHooks>false</displayAllHooks>
		<strictCucumber6Behavior>true</strictCucumber6Behavior>
	</configuration>
</plugin>
```

To use ExtentReports version 4 use the plugin version 1.6.0 in the POM. Refer to **Plugin Configuration** section in the [article](https://ghchirp.tech/2114/). 


The Cucumber Json report is created with the below **setting in the Cucumber runner**. Refer to **Cucumber JSON Formatter Setup** section in the [article](https://ghchirp.tech/2114/).
```
@CucumberOptions(plugin = { "json:target/json-report/cucumber.json"})
```

**Plugin Configuration** -
| Property                        | Type      | Default | Description                                     |                       |
| ----------------------------    |:---------:|:-------:|-------------------------------------------------|-----------------------|               
| **cucumberJsonReportDirectory** | mandatory |         | directory path of the JSON report               |                       |
| **extentPropertiesDirectory**   | optional  |         | directory path of the project extent.properties |                       |
| **displayAllHooks**             | optional  | false   | flag for all hooks are to be displayed          |                       |
| **strictCucumber6Behavior**     | optional  | true    | flag to set undefined step to failed            | Available after 2.0.0 |

Refer to **Plugin Configuration** section in the [article](https://ghchirp.tech/2114/).

**Report Settings** - The Spark and Json reporter is enabled by default. The Pdf reporter have to be enabled in the project extent.properties file by setting the report start property to true. Refer to **ExtentReport Settings** section in the [article](https://ghchirp.tech/2114/).

