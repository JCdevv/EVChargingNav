package uk.ac.mmu.advprog.kotlinmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import androidx.fragment.app.Fragment
import it.beppi.tristatetogglebutton_library.TriStateToggleButton
import it.beppi.tristatetogglebutton_library.TriStateToggleButton.OnToggleChanged
import it.beppi.tristatetogglebutton_library.TriStateToggleButton.ToggleStatus



class FragmentOne : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.fragmentone,container,false)
        var btnFrag : Button = view.findViewById(R.id.btnFrag)
        var btnFrag2 : Button = view.findViewById(R.id.btnFrag2)


        var freeToggle : TriStateToggleButton = view.findViewById(R.id.tstb_1)
        var streetToggle : TriStateToggleButton = view.findViewById(R.id.tstb_2)
        var singleToggle : TriStateToggleButton = view.findViewById(R.id.tstb_3)
        var tripleToggle : TriStateToggleButton = view.findViewById(R.id.tstb_4)
        var dcToggle : TriStateToggleButton = view.findViewById(R.id.tstb_5)

        //If switch is in off (red) position, isfree = 1 (user does not want to see free points)
        //If switch is in mid position (not chosen), isfree = 0 (user does not want to filter based on free points)
        //If switch is in on (green) position, isfree = 2, (user wants to see free points only)
        //The above is same for each toggle
        freeToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> Data.isfree = 1
                ToggleStatus.mid -> Data.isfree = 0
                ToggleStatus.on -> Data.isfree = 2
            }
        })

        streetToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> Data.onstreet = 1
                ToggleStatus.mid -> Data.onstreet = 0
                ToggleStatus.on -> Data.onstreet = 2
            }
        })

        singleToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> Data.onephase = 1
                ToggleStatus.mid -> Data.onephase = 0
                ToggleStatus.on -> Data.onephase = 2
            }
        })

        tripleToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> Data.triplephase = 1
                ToggleStatus.mid -> Data.triplephase = 0
                ToggleStatus.on -> Data.triplephase = 2
            }
        })

        dcToggle.setOnToggleChanged(OnToggleChanged { toggleStatus, _, _ ->
            when (toggleStatus) {
                ToggleStatus.off -> Data.dc = 1
                ToggleStatus.mid -> Data.dc = 0
                ToggleStatus.on -> Data.dc = 2
            }
        })

        //Opens maps fragment
        btnFrag.setOnClickListener( View.OnClickListener {

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,MapsFragment()).addToBackStack(null).commit()


        })

        //Opens settings fragment
        btnFrag2.setOnClickListener( View.OnClickListener {

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,ConfigFragment()).addToBackStack(null).commit()

        })

        return view
    }
}