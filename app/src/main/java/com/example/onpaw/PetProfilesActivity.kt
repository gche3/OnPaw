package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PetProfilesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (user.petList.isEmpty()) {
            setContentView(R.layout.pet_profiles_empty)

            findViewById<ImageView>(R.id.pet_profiles_back).setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.pet_profiles_add).setOnClickListener {
                val newPet = Pet()
                user.petList.add(newPet)
                val idx = userList.indexOfFirst { it.email == user.email }
                if (idx != -1) {
                    userList[idx].petList = user.petList
                }
                val intent = Intent(this, IndividualPetActivity::class.java)
                intent.putExtra("profileIdx", user.petList.size - 1)
                startActivity(intent)
            }

        } else {
            setContentView(R.layout.pet_profiles)

            val inflater = LayoutInflater.from(this)

            for (i in user.petList.indices) {
                val linearParent = findViewById<LinearLayout>(R.id.pet_list_container)
                val newPetProfile = inflater.inflate(R.layout.pet_profile_list_item, linearParent, false)
                newPetProfile.findViewById<TextView>(R.id.pet_name).text = user.petList[i].name
                newPetProfile.findViewById<ImageView>(R.id.pet_icon).setImageResource(user.petList[i].icon)

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
                user.petList.add(newPet)
                val idx = userList.indexOfFirst { it.email == user.email }
                if (idx != -1) {
                    userList[idx].petList = user.petList
                }
                val intent = Intent(this, IndividualPetActivity::class.java)
                intent.putExtra("profileIdx", user.petList.size - 1)
                startActivity(intent)
            }
        }
    }
}
