 # Installing logos

 - Copy all images to the specified location:

        [glassfish_directory]/domains/domain1/docroot/logos/
        

- Copy the `dataverse.default.properites` file to your `~/.dataverse/` folder, and change the file name to `dataverse.properties` (if you haven't already done so)


- Edit the `~/.dataverse/dataverse.properites` file by adding the path to the footer HTMl file in the line `FooterCustomizationFile=`:

        FooterCustomizationFile=[path_to_html_file]
