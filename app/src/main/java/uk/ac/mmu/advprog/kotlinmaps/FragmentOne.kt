package uk.ac.mmu.advprog.kotlinmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import it.beppi.tristatetogglebutton_library.TriStateToggleButton
import it.beppi.tristatetogglebutton_library.TriStateToggleButton.OnToggleChanged
import it.beppi.tristatetogglebutton_library.TriStateToggleButton.ToggleStatus

class FragmentOne : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.fragmentone,container,false)
        var mapsButton : Button = view.findViewById(R.id.btnFrag)
        var settingsButton : Button = view.findViewById(R.id.btnFrag2)
        var helpButton : Button = view.findViewById(R.id.btnFrag3)

        var freeToggle : TriStateToggleButton = view.findViewById(R.id.tstb_1)
        var streetToggle : TriStateToggleButton = view.findViewById(R.id.tstb_2)
        var singleToggle : TriStateToggleButton = view.findViewById(R.id.tstb_3)
        var tripleToggle : TriStateToggleButton = view.findViewById(R.id.tstb_4)
        var dcToggle : TriStateToggleButton = view.findViewById(R.id.tstb_5)

        var freeText : TextView = view.findViewById(R.id.textView9)
        var streetText : TextView = view.findViewById(R.id.textView10)
        var singleText : TextView = view.findViewById(R.id.textView11)
        var threeText : TextView = view.findViewById(R.id.textView12)
        var dcText : TextView = view.findViewById(R.id.textView13)

        //If switch is in off (red) position, isfree = 1 (user does not want to see free points)
        //If switch is in mid position (not chosen), isfree = 0 (user does not want to filter based on free points)
        //If switch is in on (green) position, isfree = 2, (user wants to see free points only)
        //The above is same for each toggle
        freeToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> {
                    Data.isfree = 1
                    freeText.setText("No")
                }
                ToggleStatus.mid -> {
                    Data.isfree = 0
                    freeText.setText("")
                }
                ToggleStatus.on -> {
                    Data.isfree = 2
                    freeText.setText("Yes")
                }
            }
        })

        streetToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> {
                    Data.onstreet = 1
                    streetText.setText("No")
                }
                ToggleStatus.mid -> {
                    Data.onstreet = 0
                    streetText.setText("")
                }
                ToggleStatus.on -> {
                    Data.onstreet = 2
                    streetText.setText("Yes")
                }
            }
        })

        singleToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> {
                    Data.onephase = 1
                    singleText.setText("No")
                }
                ToggleStatus.mid -> {
                    Data.onephase = 0
                    singleText.setText("")
                }
                ToggleStatus.on -> {
                    //If either of the other charging method toggles are set to "Yes", do not allow this toggle to also be set to yes.
                    if(tripleToggle.toggleStatus == ToggleStatus.on || dcToggle.toggleStatus == ToggleStatus.on) {
                        Toast.makeText(activity!!.applicationContext,"Only choose one of either DC, Single or Triple",Toast.LENGTH_LONG).show()
                        singleToggle.toggleStatus = ToggleStatus.mid
                    }
                    else{
                        Data.onephase = 2
                        singleText.setText("Yes")
                    }
                }
            }
        })

        tripleToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> {
                    Data.triplephase = 1
                    threeText.setText("No")
                }
                ToggleStatus.mid -> {
                    Data.triplephase = 0
                    threeText.setText("")
                }
                ToggleStatus.on -> {
                    //If either of the other charging method toggles are set to "Yes", do not allow this toggle to also be set to yes.
                    if(singleToggle.toggleStatus == ToggleStatus.on || dcToggle.toggleStatus == ToggleStatus.on) {
                        Toast.makeText(activity!!.applicationContext,"Only choose one of either DC, Single or Triple",Toast.LENGTH_LONG).show()
                        tripleToggle.toggleStatus = ToggleStatus.mid
                    }
                    else {
                        Data.triplephase = 2
                        threeText.setText("Yes")
                    }
                }
            }
        })

        dcToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> {
                    Data.dc = 1
                    dcText.setText("No")
                }
                ToggleStatus.mid -> {
                    Data.dc = 0
                    dcText.setText("")
                }
                ToggleStatus.on -> {
                    //If either of the other charging method toggles are set to "Yes", do not allow this toggle to also be set to yes.
                    if(tripleToggle.toggleStatus == ToggleStatus.on || singleToggle.toggleStatus == ToggleStatus.on) {
                        Toast.makeText(activity!!.applicationContext,"Only choose one of either DC, Single or Triple",Toast.LENGTH_LONG).show()
                        dcToggle.toggleStatus = ToggleStatus.mid
                    }
                    else {
                        Data.dc = 2
                        dcText.setText("Yes")
                    }
                }
            }
        })

        //Opens maps fragment
        mapsButton.setOnClickListener( View.OnClickListener {

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,MapsFragment()).addToBackStack(null).commit()


        })

        //Opens settings fragment
        settingsButton.setOnClickListener( View.OnClickListener {

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,ConfigFragment()).addToBackStack(null).commit()

        })

        //Opens help fragment
        helpButton.setOnClickListener( View.OnClickListener {

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,HelpFragment()).addToBackStack(null).commit()

        })

        return view
    }
}