//package ch.supsi.dti.isin.meteoapp.fragments;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//
//import ch.supsi.dti.isin.meteoapp.R;
//import ch.supsi.dti.isin.meteoapp.model.Location;
//
//public class ShowDialogFragment extends Fragment {
//    Button mShowButton;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_list, container, false);
//
//        mShowButton = view.findViewById(R.id.menu_add);
//        mShowButton.setOnClickListener((v) -> {
//            FragmentManager manager = getFragmentManager();
//            ListFragment dialog = ListFragment.newInstance(new Location());
//            dialog.setTargetFragment(ShowDialogFragment.this, 0);
//            dialog.show(manager, "TestDialog");
//        });
//
//        return view;
//    }
//}
