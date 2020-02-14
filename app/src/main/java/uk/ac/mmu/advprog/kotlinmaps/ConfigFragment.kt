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
        var check1 : CheckBox = view.findViewById(R.id.checkBox1)
        var check2 : CheckBox = view.findViewById(R.id.checkBox2)
        var check3 : CheckBox = view.findViewById(R.id.checkBox3)
        var check4 : CheckBox = view.findViewById(R.id.checkBox4)
        var check5 : CheckBox = view.findViewById(R.id.checkBox5)
        var subBut : Button = view.findViewById(R.id.submitButton)
        var subBut2 : Button = view.findViewById(R.id.button2)
        var checkCount = 0;

        check1.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount++
            }
            else{
                checkCount--
            }
        })

        check2.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount++
            }
            else{
                checkCount--
            }
        })

        check3.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount++
            }
            else{
                checkCount--
            }
        })


        subBut.setOnClickListener( View.OnClickListener {

            var context: Context = activity!!.applicationContext
            var db = DatabaseHelper(context)

            // 1 = every day
            // 3 = every 3 days
            // 0 = every startup
            if(checkCount == 1){
                if(check1.isChecked){
                    db.setUpdate(1)
                }else if(check2.isChecked){
                    db.setUpdate(3)
                }else if(check3.isChecked){
                    db.setUpdate(0)
                }
            }else{
                Toast.makeText(this.context,"Please Only Choose One Option",Toast.LENGTH_SHORT)
            }

        })

        subBut2.setOnClickListener( View.OnClickListener {

            var context: Context = activity!!.applicationContext
            var db = DatabaseHelper(context)

            if(check1.isChecked && check2.isChecked || !check1.isChecked && !check2.isChecked){

            }
            else{

            }

        })


        return view
    }
}
