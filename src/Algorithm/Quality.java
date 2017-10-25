package Algorithm;

public class Quality
{
	int New;
	int Cost;
	float quality;
	public Quality(int New,int Cost)
	{
		this.New=New;
		this.Cost=Cost;
		this.quality=(float)New/Cost;
	}
}
