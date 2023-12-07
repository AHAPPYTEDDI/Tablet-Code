using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.XR.ARFoundation;


//This script controls the first panel of the screen, when the user first enters the application

public class ActivateButton : MonoBehaviour
{
    [SerializeField]
    public GameObject panelParent;
    public ARPlaneManager arPlaneManager;

    protected bool ActivatePlaneDetection = false;

    [SerializeField]
    public Button button;


    [SerializeField]
    public ScannerBLE scanner;

    private bool solesActive;

    //cam1 enabled allows 3D soles to be shown without device camera showing in the background
    public Camera cam1;

    //cam2 is the main camera for the AR experience
    public Camera cam2;
    public GameObject instructionPanel;

    // Start is called before the first frame update

    
    void Start()
    {
        // OnStart, AR instruction panel is not shown. 
        instructionPanel.gameObject.SetActive(false);

        cam1.enabled = true;
        cam2.enabled = false;

        //disable continue button (on instruction panel) onStart
        button.interactable = false;

        //disable planeDetection
        arPlaneManager.enabled = !arPlaneManager.enabled;
    }

    // Update is called once per frame
    void Update()
    {
        AreSolesActive();
    }

    public void AreSolesActive()
    {
        //check if smart insoles are active
        solesActive = scanner.isConnected;
        if (solesActive)
        {
            //Make pairing insoles button interactable, and change button text to notify user they can continue to AR Visualization
            button.interactable = true;
            button.GetComponentInChildren<TMPro.TextMeshProUGUI>().text = "Continue to AR Visualization";

            //deactivate script
            enabled = false;
        }
    }


    public void HidePanel()
    {
        //Activate instructions panel
        instructionPanel.gameObject.SetActive(true);

        //Change camera
        cam1.enabled = !cam1.enabled;
        cam2.enabled = !cam2.enabled;

        //Deactivate panel
        panelParent.gameObject.SetActive(false);
        
        //Plane detection activated
        ActivatePlaneDetection = true;

        //enable plane detection
        arPlaneManager.enabled = !arPlaneManager.enabled;


    }


}