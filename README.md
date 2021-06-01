# jUngood
Utility to select roms by language and other criteria in GoodSets
Usage : java -jar jUngood.jar "path/to/roms/folder/" options
The roms folder must contain individual roms, unziped or ziped one by one.
Options :
 -delete : applies the selection and deletes unwanted roms from your hard drive. If omitted, nothing is really done, you can safely preview selection in the output.
 -english : selects english language versions, if omitted it will be french language.
 -keeppd : keeps Public Domain roms. They are deleted by default.
 -verbose : for debug purpose.
