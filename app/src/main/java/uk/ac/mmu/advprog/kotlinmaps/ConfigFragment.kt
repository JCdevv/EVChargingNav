package uk.ac.mmu.advprog.kotlinmaps

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.fragment.app.Fragment



class ConfigFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.loadingfragment,container,false)


        //RENAME BELOW
        var check1 : CheckBox = view.findViewById(R.id.checkBox1)
        var check2 : CheckBox = view.findViewById(R.id.checkBox2)
        var check3 : CheckBox = view.findViewById(R.id.checkBox3)
        var subBut : Button = view.findViewById(R.id.submitButton)
        var checkCount = 0;

        check1.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount--
            }
            else{
                checkCount++
            }
        })

        check2.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount--
            }
            else{
                checkCount++
            }
        })

        check3.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                checkCount--
            }
            else{
                checkCount++
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
                //show error
            }

        })

        return view
    }
}