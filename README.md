# NewAsyncTasks
New class for use instead of deprecated Android AsyncTask based on Android LiveData

При поддержке и рефакторинге легаси приложений на Java использующих старые AsyncTask И при этом требующих обновления ui в процессе
выполнения длительного действия (progress bar)  переписать их на современный стэк с использованием Kotlin корутин, Flow не представляется
возможным. Данный класс решает эту проблему за счет использования LiveData.  
