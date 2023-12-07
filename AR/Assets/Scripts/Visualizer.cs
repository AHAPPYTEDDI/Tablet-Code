using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Visualizer : MonoBehaviour
{
    [SerializeField] bool leftDevice;
    [SerializeField] ScannerBLE scannerBLE;
    [SerializeField] GameObject[] lights = new GameObject[40];

    public Material blueMaterial;
    public Material greenMaterial;
    public Material redMaterial;
    public Material transparent;
    public Material heatMaterial;

    private Renderer rend;

    private bool AuraVisualization;
    private bool HeatMapVisualization;
    public GameObject ChangeVizButtonParent;



    private void Start()
    {
        rend = GetComponent<Renderer>();

    }

    void Update()
    {
        AuraVisualization = ChangeVizButtonParent.GetComponent<ChangeViz>().AuraVisualization;
        HeatMapVisualization = ChangeVizButtonParent.GetComponent<ChangeViz>().HeatVisualization;

        if (AuraVisualization)
        {
            DisplayAuraVisualization();
            for (int i = 0; i < lights.Length; i++)
            {
                lights[i].SetActive(false);
            }

        }

        else if (HeatMapVisualization)
        {
            DisplayHeatMapVisualization();

            for (int i = 0; i < lights.Length; i++)
            {
                lights[i].SetActive(false);
            }
        }

        else
        {

            for (int i = 0; i < lights.Length; i++)
            {
                lights[i].SetActive(true);
            }
            DisplayRegularVisualization();
        }

    }


    public void DisplayAuraVisualization()
    {
        rend.material = blueMaterial;
    }

    public void DisplayHeatMapVisualization()
    {

        rend.material = heatMaterial;
    }


    public void DisplayRegularVisualization()
    {
        if (leftDevice)
        {
            rend.material = transparent;
            for (int i = 0; i < scannerBLE.SensorDataLeft.Length; i++)
            {
                lights[i].GetComponent<Renderer>().material.color = new Color(scannerBLE.SensorDataLeft[i] / 255f, 0, 0);
            }

            //int LFrontAverage = getFrontAverage(scannerBLE.SensorDataLeft);
            //int LBackAverage = getBackAverage(scannerBLE.SensorDataLeft);



            //if (LFrontAverage > 0 && LFrontAverage <= 100 && LBackAverage > 0 && LBackAverage <= 100)
            //{
            //    rend.material = greenMaterial;
            //}
            //else if (LFrontAverage > 100 || LBackAverage > 100)
            //{
            //    rend.material = redMaterial;
            //}
            //else if (LFrontAverage == 0 && LBackAverage == 0)
            //{
            //    rend.material = transparent;
            //}

        }
        else
        {
            rend.material = transparent;
            for (int i = 0; i < scannerBLE.SensorDataRight.Length; i++)
            {
                lights[i].GetComponent<Renderer>().material.color = new Color(scannerBLE.SensorDataRight[i] / 255f, 0, 0);
            }
            //int RFrontAverage = getFrontAverage(scannerBLE.SensorDataRight);
            //int RBackAverage = getBackAverage(scannerBLE.SensorDataRight);

            //if (RFrontAverage > 0 && RFrontAverage <= 100 && RBackAverage > 0 && RBackAverage <= 100)
            //{
            //    rend.material = greenMaterial;
            //}
            //else if (RFrontAverage > 100 || RBackAverage > 100)
            //{
            //    rend.material = redMaterial;
            //}
            //else if (RBackAverage == 0 && RBackAverage == 0)
            //{
            //    rend.material = transparent;
            //}
        }
    }

    public int getFrontAverage(int[] sensorData)
    {
        int average = 0;

        for (int i = 0; i < 22; i++)
        {
            average += sensorData[i];
        }

        average /= 23;

        return average;
    }

    public int getBackAverage(int[] sensorData)
    {
        int average = 0;

        for (int i = 32; i < 40; i++)
        {
            average += sensorData[i];
        }

        average /= 9;

        return average;
    }
}
