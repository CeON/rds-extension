
# rds-extension
Social Data Repository extensions that can plugged into dataverse instance

## RDS theme

Theme files can be found under `src/main/theme` directory.
The main file that is taken as the theme file is `theme.css` and it should `@import "theme-base.scss"` to extend it.

> Note that scss compiler requires the original theme files, so Maven will copy them for you during build,
> then copy over RDS files possibly overriding original ones and run the compiler on the result. 
> Everything is done automatically during Maven build, just be aware when adding files that you can use
> and overwrite files from `resources/velcer` directory.
