package uk.ac.mmu.advprog.kotlinmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HelpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.helpfragment,container,false)
        var subBtn : Button = view.findViewById(R.id.rtnBtn)

        //returns back to filter fragment
        subBtn.setOnClickListener( View.OnClickListener {

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,FilterFragment()).addToBackStack(null).commit()


        })

        return view
    }

}