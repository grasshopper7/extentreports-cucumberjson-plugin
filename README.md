This deals with **generating Extent reports for Cucumber-JVM**, version 4.3 and above, by using a **Maven Plugin which parses the Cucumber JSON report**. For more details refer to this [article](https://grasshopper.tech/2114/).

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
