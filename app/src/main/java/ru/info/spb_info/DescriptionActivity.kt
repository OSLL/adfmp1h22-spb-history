package ru.info.spb_info

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.description_layout.*

class DescriptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.description_layout)
        val descriptionText = "Дом компании \"Зингер\"" +
                "модерн\n" +
                "\n" +
                "наб. канала Грибоедова, 21\n" +
                "\n" +
                "Пам. арх. (регион.)\n" +
                "\n" +
                "Комедиантский театральный оперный дом  (сгорел в 1749 г.)\n" +
                "\n" +
                "1740-е -\n" +
                "\n" +
                "Дом И. И. Панфилова  (не сохран.)\n" +
                "\n" +
                "1770-е - трехэтажный дом\n" +
                "\n" +
                "Дом К. И. Имзена\n" +
                "\n" +
                "1840-е - перестройка, надстроен 4-й этаж\n" +
                "\n" +
                "Дом компании \"Зингер\"\n" +
                "\n" +
                "1902-1904 - арх. Сюзор Павел Юльевич - новое здание\n" +
                "\n" +
                " \n" +
                "\n" +
                "Дом книги" +
                "Дом компании \"Зингер\"" +
                "модерн\n" +
                "\n" +
                "наб. канала Грибоедова, 21\n" +
                "\n" +
                "Пам. арх. (регион.)\n" +
                "\n" +
                "Комедиантский театральный оперный дом  (сгорел в 1749 г.)\n" +
                "\n" +
                "1740-е -\n" +
                "\n" +
                "Дом И. И. Панфилова  (не сохран.)\n" +
                "\n" +
                "1770-е - трехэтажный дом\n" +
                "\n" +
                "Дом К. И. Имзена\n" +
                "\n" +
                "1840-е - перестройка, надстроен 4-й этаж\n" +
                "\n" +
                "Дом компании \"Зингер\"\n" +
                "\n" +
                "1902-1904 - арх. Сюзор Павел Юльевич - новое здание\n" +
                "\n" +
                " \n" +
                "\n" +
                "Дом книги"

        building_description.text = descriptionText
        building_description.movementMethod = ScrollingMovementMethod()
    }
}