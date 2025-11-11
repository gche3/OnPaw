package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Pet(
    var name: String = "",
    var species: String = "",
    var age: Int = 0,
    var note: String = "",
    var icon: Int = R.drawable.paw
)

var petList: MutableList<Pet> = mutableListOf()

class PetProfilesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (petList.isEmpty()) {
            setContentView(R.layout.pet_profiles_empty)

            findViewById<ImageView>(R.id.pet_profiles_back).setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.pet_profiles_add).setOnClickListener {
                val newPet = Pet()
                petList.add(newPet)
                val intent = Intent(this, IndividualPetActivity::class.java)
                intent.putExtra("profileIdx", petList.size - 1)
                startActivity(intent)
            }

        } else {
            setContentView(R.layout.pet_profiles)

            val inflater = LayoutInflater.from(this)

            for (i in petList.indices) {
                val linearParent = findViewById<LinearLayout>(R.id.pet_list_container)
                val newPetProfile = inflater.inflate(R.layout.pet_profile_list_item, linearParent, false)
                newPetProfile.findViewById<TextView>(R.id.pet_name).text = petList[i].name
                newPetProfile.findViewById<ImageView>(R.id.pet_icon).setImageResource(petList[i].icon)

                linearParent.addView(newPetProfile)

                newPetProfile.setOnClickListener {
                    val intent = Intent(this, IndividualPetActivity::class.java)
                    intent.putExtra("profileIdx", i)
                    startActivity(intent)
                }
            }

            findViewById<ImageView>(R.id.pet_profiles_back).setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.pet_profiles_add).setOnClickListener {
                val newPet = Pet()
                petList.add(newPet)
                val intent = Intent(this, IndividualPetActivity::class.java)
                intent.putExtra("profileIdx", petList.size - 1)
                startActivity(intent)
            }
        }
    }
}
