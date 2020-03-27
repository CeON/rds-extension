 # Installing logos

 - Copy all images to the specified location:

        [glassfish_directory]/domains/domain1/docroot/logos/


- Configure your Dataverse installation to make use of the supplied `footer_logos_rds.html` file:

        curl -X PUT -d 'footer_logos_rds.html' [dataverse_web_address]/api/admin/settings/:FooterCustomizationFile