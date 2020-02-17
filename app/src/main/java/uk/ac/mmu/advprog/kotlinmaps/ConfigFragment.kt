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


        //RENAME BELOW
        var dailyCheck : CheckBox = view.findViewById(R.id.checkBox1)
        var threeCheck : CheckBox = view.findViewById(R.id.checkBox2)
        var everydayCheck : CheckBox = view.findViewById(R.id.checkBox3)
        var govCheck : CheckBox = view.findViewById(R.id.checkBox4)
        var openCheck : CheckBox = view.findViewById(R.id.checkBox5)
        var scheduleSubmit : Button = view.findViewById(R.id.submitButton)
        var subBut2 : Button = view.findViewById(R.id.button2)
        var checkCount = 0;

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
            if(checkCount == 1){
                if(dailyCheck.isChecked){
                    db.setUpdate(1)
                }else if(threeCheck.isChecked){
                    db.setUpdate(3)
                }else if(everydayCheck.isChecked){
                    db.setUpdate(0)
                }
            }else{
                Toast.makeText(this.context,"Please Only Choose One Option",Toast.LENGTH_SHORT)
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
                    db.setSource(1)
                    db.emptyTables()
                    var gm = MainActivity().getMarkers()
                    gm.execute()

                }
                else if(openCheck.isChecked){
                    db.setSource(2)
                    db.emptyTables()
                    var gm = MainActivity().getOpenMarkers()
                    gm.execute()
                }
            }

        })


        return view
    }
}
