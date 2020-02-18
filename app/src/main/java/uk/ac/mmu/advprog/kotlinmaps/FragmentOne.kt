package uk.ac.mmu.advprog.kotlinmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import android.widget.CompoundButton

class FragmentOne : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.fragmentone,container,false)
        var btnFrag : Button = view.findViewById(R.id.btnFrag)
        var btnFrag2 : Button = view.findViewById(R.id.btnFrag2)
        var freeSwitch : Switch = view.findViewById(R.id.switch1)
        var streetSwitch : Switch = view.findViewById(R.id.switch3)
        var onephaseSwitch : Switch = view.findViewById(R.id.switch2)
        var triplephaseSwitch : Switch = view.findViewById(R.id.switch4)
        var dcSwitch : Switch = view.findViewById(R.id.switch5)

        freeSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked == true){
                Data.isfree = true
            }

        })

        streetSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked == true){
                Data.onstreet = true
            }

        })

        onephaseSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked == true){
                Data.onephase = true
            }

        })

        triplephaseSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{buttonView, isChecked ->
            if(isChecked == true){
                Data.triplephase = true
            }
        })

        dcSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked == true){
                Data.dc = true
            }

        })


        btnFrag.setOnClickListener( View.OnClickListener {

            var mp = this.activity as MainActivity

            mp.adapter?.addFragment(MapsFragment(), "Fragment Two")

            mp.viewPager?.adapter = mp.adapter
            mp.setViewPager(1)

        })

        btnFrag2.setOnClickListener( View.OnClickListener {

            var mp = this.activity as MainActivity

            mp.adapter?.addFragment(ConfigFragment(), "Fragment Three")

            mp.viewPager?.adapter = mp.adapter
            mp.setViewPager(2)

        })

        return view
    }
}