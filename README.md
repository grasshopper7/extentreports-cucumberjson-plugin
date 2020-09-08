This deals with **generating Extent reports for Cucumber-JVM**, version 4.3 and above, by using a **Maven Plugin which parses the Cucumber JSON report**. For more details refer to this [article](https://grasshopper.tech/2114/).

The **plugin version 2.0.0** uses **ExtentReport version 5** which deprecates many reporters other than **Spark, Json and Klov**. This plugin also includes a **custom [Pdf reporter](https://github.com/grasshopper7/extentreports-pdf-dashboard-reporter)** which generates a dashboard of the test run. Use the below **configuration** for the plugin.


```
<plugin>
	<groupId>tech.grasshopper</groupId>
	<artifactId>extentreports-cucumberjson-plugin</artifactId>
	<version>2.0.0</version>
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

To use ExtentReports version 4 use the plugin version 1.6.0 in the POM.


The Cucumber Json report is created with the below **setting in the Cucumber runner**.
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

**Report Settings** - The Spark reporter is enabled by default. The Json and Pdf reporter have to be enabled in the project extent.properties file.

