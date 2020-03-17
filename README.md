
# rds-extension
Social Data Repository extensions that can plugged into dataverse instance

## RDS theme installation

 - First you need to package the project using maven

       cd <REPO_DIR>
       mvn package

   It will produce zip artifact inside target directory: `rds-extension-<VERSION>-assets.zip`

 - Next you need to extract contents of this zip file inside docroot directory of glassfish domain where you have dataverse deployed

       unzip target/rds-extension-<VERSION>-assets.zip -d /tmp/
       mv /tmp/rds-extension-<VERSION>/assets <GLASSFISH_DOMAIN_DIR>/docroot/
       rm -r /tmp/rds-extension-<VERSION>

 - At last you will need to tell dataverse that you will be using custom theme by modifying configuration param: `CustomThemeCssFilename` to a value: `theme_rds.css`



> Note that this theme is generated using scss compiler using original scss theme by overriding some of the variables. 
> Currently scss compiler depends on the existence of the original theme scss inside source code of this repository. As a result it is required to copy original theme scss to this repository when it changes. 

