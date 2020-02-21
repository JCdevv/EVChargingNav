package uk.ac.mmu.advprog.kotlinmaps

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment

class ConfigFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.fragment_config,container,false)

        //Get local variable of all checkboxes, buttons etc
        var dailyCheck : CheckBox = view.findViewById(R.id.checkBox1)
        var threeCheck : CheckBox = view.findViewById(R.id.checkBox2)
        var everydayCheck : CheckBox = view.findViewById(R.id.checkBox3)
        var govCheck : CheckBox = view.findViewById(R.id.checkBox4)
        var openCheck : CheckBox = view.findViewById(R.id.checkBox5)
        var scheduleSubmit : Button = view.findViewById(R.id.submitButton)
        var subBut2 : Button = view.findViewById(R.id.button2)
        var checkCount = 0;

        //When a check box is clicked, increment or decrease check counter
        dailyCheck.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount++
            }
            else{
                checkCount--
            }
        })

        threeCheck.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount++
            }
            else{
                checkCount--
            }
        })

        everydayCheck.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount++
            }
            else{
                checkCount--
            }
        })


        scheduleSubmit.setOnClickListener( View.OnClickListener {

            var context: Context = activity!!.applicationContext
            var db = DatabaseHelper(context)

            // 1 = every day
            // 3 = every 3 days
            // 0 = every startup

            //only submit if checkcount is 1, if check count is greater than one, more than one box has been checked. Lower and not checked at all.
            if(checkCount == 1){
                if(dailyCheck.isChecked){
                    db.setUpdate(1)
                }else if(threeCheck.isChecked){
                    db.setUpdate(3)
                }else if(everydayCheck.isChecked){
                    db.setUpdate(0)
                }
            }else{
                Toast.makeText(this.context,"Please Only Choose One Option",Toast.LENGTH_SHORT).show()
            }
        })

        subBut2.setOnClickListener( View.OnClickListener {

            var context: Context = activity!!.applicationContext
            var db = DatabaseHelper(context)

            if(govCheck.isChecked && openCheck.isChecked || !govCheck.isChecked && !openCheck.isChecked){
                //do nothing
            }
            else{
                if(govCheck.isChecked){
                    //1 used to represent using the gov data source
                    db.setSource(1)
                }
                else if(openCheck.isChecked){
                    //2 used to represent openchargemap data source
                    db.setSource(2)
                }

                //set Data singleton isupdating var to true, so loading fragment updates
                Data.isupdating = true

                //Display loading fragment
                var fragTransation = fragmentManager!!.beginTransaction()
                fragTransation.replace(android.R.id.content,LoadingFragment())
                fragTransation.commit()
            }
        })
        return view
    }
}
