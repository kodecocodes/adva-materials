# ANDROID TUTORIAL, TEMPLATE APP PROJECT

## SUMMARY

This project is a barebones Android Studio project that implements the code style and formatting standards for raywenderlich.com Android Tutorial Sample Projects.

<img src="img/working_gif.gif" height="480"/>

The items implemented are:

- Correct format for the package name
- An RW launcher icon
- A customized RW Splash screen
- A specific RW code style
- A specific RW copyright profile

## HOW TO USE THE TEMPLATE APP PROJECT

You need to make a few edits and also set up your Code Style.

### ANDROID STUDIO CODE STYLE

**IMPORTANT:** Android Studio allows editing the default code style at the IDE-level, even creating your own "named" IDE-level code style profiles. If you have a modified `Default` IDE-level profile in your Android Studio or if you use your own "named" IDE-level code style profile and you open this project, all project-level code style and copyright settings in this project will be removed and your own settings will be applied. 

To prevent this, please follow along with the following steps, depending on your scenario:

- Check to see if your `Default` IDE-level code style is modified: 

  - Open Android Studio with any project except this one. Then navigate to `Preferences>Editor>Code Style` and check if your code scheme has `Default` in **BLUE** color (this means it is modified).

    ![modified_default](img/modified_default.png)

  - If `Default` is modified (colored **BLUE**), then:

    - If you wish to save your code style changes, click on the cog icon and select `Duplicate`. Then, give this copy a meaningful name. Be sure to select the `Default` again because you are about to reset it!

    - After you've duplicated your changes (if you want to preserve them) or if you don't wish to preserve your changes, click on the cog icon and select `Restore Default`.

      ![restore_default](img/restore_default.png)

    - Next, accept the change.

      ![accept_change](img/accept_change.png)

    - Once done, you will see your Default is no longer colored **BLUE**.

      ![unmodified_default](img/unmodified_default.png)

- Check to see if you're using your own "named" IDE-level code style:
  
  - Open Android Studio with any project except this one. Then navigate to `Preferences>Editor>Code Style` and see if you are using a "named" IDE-level code style. If you have anything other than `Default` selected under "Stored in IDE", you are using a named IDE-level profile.

    ![using_named_code_style](img/using_named_code_style.png)

  - To temporarily stop using a "named" IDE-level profile, simply select `Default` to go back to using Android Studio's default. You can always switch back once you're done with your tutorial.


- Once you've either reset a `Default` code style or selected `Default` if you were using a custom profile, go ahead and open the template and all the project-level settings should remain intact.

### EDIT THE PROJECT TO MAKE IT YOUR OWN

Several pieces should be changed to unique values such that they are relevant to the tutorial under development, including:

- Go to the root of the project folder and delete `img` folder (this contains images which are shown here in the README.md and are not necessary in your tutorial.)

- Change the colors in `colors.xml` to whatever you want for your tutorial, however, you are encouraged to use the RW Color Scheme in this project unless your tutorial specifically needs to use something else.

- If you are planning to use git and GitHub to manage your sample project development, be sure to start with the .gitignore file on the Android Team Guide [here](https://www.raywenderlich.com/tutorial-team/android-team-guide). However, don't forget that once done, you sill need to package the Sample App (both starter and final) in a zip file that does not include any GIT artifacts!

## License

```
Copyright (c) 2020 Razeware LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
distribute, sublicense, create a derivative work, and/or sell copies of the
Software in any work that is designed, intended, or marketed for pedagogical or
instructional purposes related to programming, coding, application development,
or information technology.  Permission for such use, copying, modification,
merger, publication, distribution, sublicensing, creation of derivative works,
or sale is expressly withheld.

This project and source code may use libraries or frameworks that are
released under various Open-Source licenses. Use of those libraries and
frameworks are governed by their own individual licenses.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
